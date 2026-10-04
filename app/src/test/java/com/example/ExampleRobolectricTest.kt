package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TruekappDatabase
import com.example.data.repository.TruekappRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @After
  fun tearDown() {
    TruekappDatabase.resetInstanceForTesting()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TRUEKAPP", appName)
  }

  @Test
  fun `recovers from stale identity hash and seeds demo data without IllegalStateException`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    TruekappDatabase.resetInstanceForTesting()

    // Simulate a pre-existing database file with the old mismatched identity hash from the crash log
    val dbFile = context.getDatabasePath(TruekappDatabase.DB_NAME)
    dbFile.parentFile?.mkdirs()
    SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { staleDb ->
      staleDb.version = 2
      staleDb.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
      staleDb.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, '8f7252a258a40f4f2d2124b16fc48894')")
    }

    val database = TruekappDatabase.getDatabase(context)
    val dao = database.truekappDao()
    val repository = TruekappRepository(dao)

    repository.seedInitialDataIfEmpty()

    assertTrue("Demo users should be seeded", dao.getUserCount() >= 5)
    assertTrue("Demo listings should be seeded", dao.getListingCount() >= 14)
    assertTrue("Demo exchanges should be seeded", dao.getExchangeCount() >= 4)

    // Verify publishing a new listing and proposing an exchange
    val camilo = dao.getUserById("user_camilo")!!
    val targetListing = dao.getListingById("list_demo_bici_celular")!!
    val offeredListing = dao.getListingById("list_camilo_01")!!

    val newExchangeId = repository.proposeExchange(
      targetListing = targetListing,
      proposer = camilo,
      proposerListing = offeredListing,
      pitchMessage = "Me interesa tu bicicleta para la universidad"
    )
    val createdExchange = repository.getExchangeById(newExchangeId)
    assertEquals("PROPOSED", createdExchange?.status)
    assertEquals(offeredListing.title, createdExchange?.proposerListingTitle)
    assertEquals(targetListing.title, createdExchange?.targetListingTitle)
  }
}
