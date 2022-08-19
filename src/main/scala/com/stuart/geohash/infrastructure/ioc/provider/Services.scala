package com.stuart.geohash.infrastructure.ioc.provider

import cats.Parallel
import cats.effect.Sync
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.domain.services.GeoHashRegister
import com.stuart.geohash.infrastructure.services.GeoPointLoader

sealed abstract class Services[F[_]] private (
  val importGeoHash: ImportGeoHash[F],
  val geoHashRegister: GeoHashRegister[F]
)

object Services {
  def make[F[_]: Sync: Parallel](repositories: Repositories[F]): Services[F] = {
    val geoHashRegister = GeoHashRegister.make[F](repositories.geoHash)

    new Services[F](
      importGeoHash = ImportGeoHash.make[F](
        loader = GeoPointLoader.make[F](),
        geoHashRegister = geoHashRegister
      ),
      geoHashRegister = GeoHashRegister.make[F](repositories.geoHash)
    ) {}
  }

}
