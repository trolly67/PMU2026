package com.example.beatles.data.remote

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "metals", strict = false)
data class CbrMetalsResponse(
    @field:ElementList(entry = "record", inline = true, required = false)
    var records: List<CbrMetalRecord> = emptyList()
)

@Root(name = "record", strict = false)
data class CbrMetalRecord(
    @field:Attribute(name = "Date", required = false) var date: String? = null,
    @field:Attribute(name = "Code", required = false) var code: String? = null,
    @field:Element(name = "Buy", required = false) var buy: String? = null,
    @field:Element(name = "Sell", required = false) var sell: String? = null
)