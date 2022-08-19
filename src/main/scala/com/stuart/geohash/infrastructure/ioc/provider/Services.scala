package com.stuart.geohash.infrastructure.ioc.provider

import cats.Parallel
import cats.effect.Sync
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.infrastructure.services.GeoPointLoader

sealed abstract class Services[F[_]] private (
  val importGeoHash: ImportGeoHash[F]
)

object Services {
  def make[F[_]: Sync: Parallel](repositories: Repositories[F]): Services[F] =
    new Services[F](
      importGeoHash = ImportGeoHash.make[F](
        repo = repositories.geoHash,
        loader = GeoPointLoader.make[F]()
      )
    ) {}
}
