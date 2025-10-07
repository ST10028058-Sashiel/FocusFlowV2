package com.st10028058.focusflowv2

import com.st10028058.focusflowv2.data.Task
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class InMemoryTaskStore {

    private val tasks = mutableListOf<Task>()

    suspend fun addTaskAndReturn(task: Task): Task {
        tasks.add(task)
        return task
    }

    fun updateTask(id: String, updated: Task) {
        val idx = tasks.indexOfFirst { it._id == id }
        if (idx != -1) tasks[idx] = updated
    }

    fun deleteTask(id: String) {
        tasks.removeAll { it._id == id }
    }

    fun getAll(): List<Task> = tasks.toList()
}

class TaskViewModelTest {

    private lateinit var store: InMemoryTaskStore

    @Before
    fun setup() {
        store = InMemoryTaskStore()
    }

    @Test
    fun `addTaskAndReturn should store task`() = runTest {
        val task = Task(
            _id = "1",
            title = "Unit Test",
            priority = "Normal",
            userId = "user"
        )

        val saved = store.addTaskAndReturn(task)

        assertNotNull(saved)
        assertEquals("Unit Test", saved.title)
        assertEquals(1, store.getAll().size)
    }

    @Test
    fun `updateTask should modify task`() = runTest {
        val original = Task(_id = "2", title = "Old", priority = "Low", userId = "user")
        store.addTaskAndReturn(original)

        val updated = original.copy(title = "New Title", priority = "High")
        store.updateTask("2", updated)

        val result = store.getAll().first { it._id == "2" }
        assertEquals("New Title", result.title)
        assertEquals("High", result.priority)
    }

    @Test
    fun `deleteTask should remove task`() = runTest {
        val toDelete = Task(_id = "3", title = "Delete Me", priority = "Normal", userId = "user")
        store.addTaskAndReturn(toDelete)

        store.deleteTask("3")

        assertTrue(store.getAll().none { it._id == "3" })
    }
}
