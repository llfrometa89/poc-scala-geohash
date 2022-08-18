package com.stuart.geohash.application.services

import cats.effect.{MonadCancelThrow, Resource}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash.GeoHashDTO

import java.io.BufferedReader

trait ImportGeoHash[F[_]] {

  def importGeoHash(file: Resource[F, BufferedReader], batch: Long): F[List[GeoHashDTO]]
}

object ImportGeoHash {

  def make[F[_]: MonadCancelThrow]: ImportGeoHash[F] = new ImportGeoHash[F] {

    def importGeoHash(file: Resource[F, BufferedReader], batch: Long): F[List[GeoHashDTO]] =
      file.use { buffer =>
        val strs = LazyList.continually(buffer.readLine()).takeWhile(_ != null)
        println(strs.mkString("\n"))
        List.empty[GeoHashDTO].pure[F]
      }
  }
}
