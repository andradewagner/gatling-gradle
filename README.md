### Features
- Teste de carga

# Teste de Carga para ambiente distribuído
![](https://gatling.io/wp-content/uploads/2019/04/Gatling-logo-2019.png)

#1- Ajuste as variáveis de ambiente abaixo no ambiente:
- PERF_TEST_JWT_PATH
  <p>Variável que aponta para o arquivo json da credencial AWS</p><br/>
- PERF_TEST_RESULTS_BUCKET_NAME
  <p>Nome do bucket onde será salvo o resultado do teste</p><br/>
- HOSTNAME
  <p>Sufixo que será anexado ao nome do arquivo. Se nao for definido um sufixo aleatório será gerado</p><br/>
- PERF_TEST_LOG_DOWNLOAD_PATH
  <p>Caminho onde os logs serao salvos ao efetuar o download do bucket S3. Se nenhum valor for informado, o caminho '<b>build/reports/downloadedLogs</b>' será utilizado</p><br/>
- ACCESS_KEY
  <p>A chave de acesso AWS</p><br/>
### - Use o comando abaixo pra gerar a imagem Docker
docker build -t wagnand/gatling:latest --label gatlingTest .

### - Use o comando para publicar a imagem no repositório
docker push wagnand/gatling:latest

### - Use o comando abaixo para executar o teste 
docker run -it wagnand/gatling:latest gradle gatlingRun-<SimulationPackage>.<Simulation>