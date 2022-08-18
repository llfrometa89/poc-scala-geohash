package com.stuart.geohash.infrastructure.stdio

object banner {

  val Banner: List[String] =
    """|  ___ _                 _       ___ _    ___ 
       | / __| |_ _  _ __ _ _ _| |_    / __| |  |_ _|
       | \__ \  _| || / _` | '_|  _|  | (__| |__ | | 
       | |___/\__|\_,_\__,_|_|  \__|   \___|____|___|
       | """.stripMargin.split("\n").toList
}
