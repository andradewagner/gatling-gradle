import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import br.com.teste.Teste._

class TesteSimulation extends Simulation {
  val httpConf = http.baseUrl(" http://192.168.49.2:30316")

  val scnTeste = scenario("Teste Kubernetes")
    .exec(teste)
    .pause(1, 3 seconds)

  setUp(
    scnTeste.inject(
      rampUsersPerSec(1).to(10000).during(5 minutes)
  )).protocols(httpConf)
}
