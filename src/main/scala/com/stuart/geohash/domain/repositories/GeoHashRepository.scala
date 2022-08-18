package com.stuart.geohash.domain.repositories

import com.stuart.geohash.domain.models.geohash.{GeoHash, GeoHashMaxPrecision}

trait GeoHashRepository[F[_]] {

  def create(geoHash: GeoHash): F[GeoHash]

  def findBy(geoHashMaxPrecision: GeoHashMaxPrecision): F[Option[GeoHash]]

  def findAll(page: Long, size: Long): F[List[GeoHash]]
}
