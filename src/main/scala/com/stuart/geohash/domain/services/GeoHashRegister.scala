package com.stuart.geohash.domain.services

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.domain.models.geohash.GeoHash
import com.stuart.geohash.domain.repositories.GeoHashRepository

trait GeoHashRegister[F[_]] {
  def register(geoHash: GeoHash): F[Unit]
}

object GeoHashRegister {

  def make[F[_]: Sync](repo: GeoHashRepository[F]): GeoHashRegister[F] = new GeoHashRegister[F] {

    def register(geoHash: GeoHash): F[Unit] = for {
      mGeoHash <- repo.findBy(geoHash.geoHash)
      _        <- Sync[F].whenA(mGeoHash.isEmpty)(repo.create(geoHash))
    } yield ()
  }
}
