package com.st10028058.focusflowv2

import com.st10028058.focusflowv2.data.Task
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class TaskViewModel {

    private val tasks = mutableListOf<Task>()

    suspend fun addTaskAndReturn(task: Task): Task? {
        tasks.add(task)
        return task
    }

    fun updateTask(id: String, updated: Task) {
        val index = tasks.indexOfFirst { it._id == id }
        if (index != -1) tasks[index] = updated
    }

    fun deleteTask(id: String) {
        tasks.removeAll { it._id == id }
    }

    fun getTasks(): List<Task> = tasks
}

class TaskViewModelTest {

    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        viewModel = TaskViewModel()
    }

    @Test
    fun `addTaskAndReturn should store task`() = runTest {
        val task = Task(_id = "1", title = "Unit Test", priority = "Normal", userId = "user")
        val saved = viewModel.addTaskAndReturn(task)
        assertNotNull(saved)
        assertEquals(1, viewModel.getTasks().size)
    }

    @Test
    fun `updateTask should modify task`() = runTest {
        val task = Task(_id = "2", title = "Old", priority = "Low", userId = "user")
        viewModel.addTaskAndReturn(task)
        viewModel.updateTask("2", task.copy(title = "New Title"))
        val updated = viewModel.getTasks().first { it._id == "2" }
        assertEquals("New Title", updated.title)
    }

    @Test
    fun `deleteTask should remove task`() = runTest {
        val task = Task(_id = "3", title = "Delete Me", priority = "Normal", userId = "user")
        viewModel.addTaskAndReturn(task)
        viewModel.deleteTask("3")
        assertTrue(viewModel.getTasks().none { it._id == "3" })
    }
}
