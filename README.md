### Features

- Teste de carga

# Teste de Carga para ambiente distribuído
![](https://gatling.io/wp-content/uploads/2019/04/Gatling-logo-2019.png)

### - Use o comando abaixo pra gerar a imagem Docker
docker build -t wagnand/gatling:latest --label gatlingTest .

### - Use o comando para publicar a imagem no repositório
docker push wagnand/gatling:latest

### - Use o comando abaixo para executar o teste 
docker run -it wagnand/gatling:latest gradle gatlingRun-<SimulationPackage>.<Simulation>