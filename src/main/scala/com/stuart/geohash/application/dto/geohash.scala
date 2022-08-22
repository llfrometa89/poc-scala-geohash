package com.stuart.geohash.application.dto

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.domain.models.geohash.{GeoHash, GeoPoint, Latitude, Longitude}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.{refineV, W}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

object geohash {

  type LatPred   = Interval.Closed[W.`-90.0d`.T, W.`90.0d`.T]
  type LongPred  = Interval.Closed[W.`-180.0d`.T, W.`180.0d`.T]
  type Latitude  = Double Refined LatPred
  type Longitude = Double Refined LongPred

  object GeoPointRefined {

    def fromLatitudeAsDouble(value: Double): Either[GeoPointError, Refined[Double, LatPred]] =
      refineV[LatPred](value).leftMap(GeoPointConversionError)

    def fromLongitudeAsDouble(value: Double): Either[GeoPointError, Refined[Double, LongPred]] =
      refineV[LongPred](value).leftMap(GeoPointConversionError)

    def fromLine[F[_]: Sync](line: String): F[ImportGeoPointDTO] =
      for {
        geoPointAsArray <- Sync[F].delay(line.toArrayByComma)
        geoPoint        <- GeoPointRefined.fromArray(geoPointAsArray)
      } yield geoPoint

    def fromArray[F[_]: Sync](values: Array[String]): F[ImportGeoPointDTO] = for {
      _                 <- Sync[F].whenA(values.size < 2)(Sync[F].raiseError(InvalidArrayConversionError))
      latitudeAsDouble  <- Sync[F].delay(values(0).toDouble)
      longitudeAsDouble <- Sync[F].delay(values(1).toDouble)
      latitude          <- Sync[F].fromEither(fromLatitudeAsDouble(latitudeAsDouble))
      longitude         <- Sync[F].fromEither(fromLongitudeAsDouble(longitudeAsDouble))
    } yield ImportGeoPointDTO(latitude, longitude)
  }

  case class ImportGeoPointDTO(latitude: Latitude, longitude: Longitude) {
    def toGeoPoint: GeoPoint = GeoPoint(
      latitude = Latitude(latitude.value),
      longitude = Longitude(longitude.value)
    )
  }

  case class GeoHashDTO(latitude: Double, longitude: Double, geoHash: String, uniquePrefix: String)

  object GeoHashDTO {

    implicit val geoHashDtoEncoder: Encoder[GeoHashDTO] = deriveEncoder[GeoHashDTO]
    implicit val geoHashDtoDecoder: Decoder[GeoHashDTO] = deriveDecoder[GeoHashDTO]

    def fromGeoHash(geoHash: GeoHash): GeoHashDTO =
      GeoHashDTO(
        latitude = geoHash.geoPoint.latitude.value,
        longitude = geoHash.geoPoint.longitude.value,
        geoHash = geoHash.geoHash.value,
        uniquePrefix = geoHash.uniquePrefix.value
      )
  }

  abstract class GeoPointError                        extends Exception
  case class GeoPointConversionError(message: String) extends GeoPointError
  case object InvalidArrayConversionError             extends GeoPointError
}
