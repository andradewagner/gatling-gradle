package br.com.teste

import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object Teste {
  class Brand(val name: String, val url: String, val body: String)

  private val global = new Brand(
    "Teste Kubernetes",
    "/",
    "teste kubernetes"
  )

  private def testeKubernetes(brand: Brand): HttpRequestBuilder =
    http(brand.name)
      .post(brand.url)
      .body(StringBody(brand.body))
      .check(status.in(200))

  val teste: HttpRequestBuilder = testeKubernetes(global)
}
