package com.example.beatles.repository

import com.example.beatles.data.GoldRateDao
import com.example.beatles.data.remote.CbrClient
import com.example.beatles.data.remote.CbrMetalsApi
import com.example.beatles.data.remote.CbrXmlParser
import com.example.beatles.data.toDomain
import com.example.beatles.data.toEntity
import com.example.beatles.domain.GoldRate
import com.example.beatles.domain.GoldRateError
import com.example.beatles.domain.GoldRateResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

class GoldRateRepository(
    private val api: CbrMetalsApi,
    private val dao: GoldRateDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    fun observeRate(): Flow<GoldRate?> = dao.observe().map { it?.toDomain() }
    suspend fun getLastKnown(): GoldRate? = dao.get()?.toDomain()

    suspend fun refresh(): GoldRateResult = withContext(Dispatchers.IO) {
        val now = clock()
        val today = LocalDate.now()
        val from = today.minusDays(30)

        try {
            val responseBody = api.getMetals(CbrClient.format(from), CbrClient.format(today))

            val rate = responseBody.byteStream().use { stream ->
                CbrXmlParser.parseGoldRate(stream, now)
            }

            if (rate == null) {
                return@withContext GoldRateResult.Error(GoldRateError.RateNotFound)
            }

            dao.upsert(rate.toEntity())
            return@withContext GoldRateResult.Success(rate)

        } catch (e: IOException) {
            return@withContext GoldRateResult.Error(GoldRateError.Offline)
        } catch (e: HttpException) {
            return@withContext GoldRateResult.Error(
                if (e.code() in 500..599) GoldRateError.ServiceUnavailable
                else GoldRateError.InvalidData
            )
        } catch (e: Exception) {
            return@withContext GoldRateResult.Error(GoldRateError.InvalidData)
        }
    }
}