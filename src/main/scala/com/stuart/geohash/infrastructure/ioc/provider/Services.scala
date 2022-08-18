package com.stuart.geohash.infrastructure.ioc.provider

import cats.effect.Sync
import com.stuart.geohash.application.services.ImportGeoHash

sealed abstract class Services[F[_]] private (
  val importGeoHash: ImportGeoHash[F]
)

object Services {
  def make[F[_]: Sync](): Services[F] =
    new Services[F](
      importGeoHash = ImportGeoHash.make[F]
    ) {}
}
