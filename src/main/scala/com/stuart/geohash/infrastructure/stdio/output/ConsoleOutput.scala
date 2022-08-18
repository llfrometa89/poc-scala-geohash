package com.stuart.geohash.infrastructure.stdio.output

import cats.Show

trait ConsoleOutput[A] {
  def show: Show[A]
}
