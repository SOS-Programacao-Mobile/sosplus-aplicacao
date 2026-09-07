# SOSPlus Aplicação

App Android em Kotlin/Compose, com cadastro e login conectados à API TypeScript e ao MySQL.

## Conectar ao backend

No repositório `sosplus-server`, inicie `docker compose up -d --build --wait`. Se estiver usando a API diretamente com Node, execute primeiro `npm run migration:run` e depois `npm run dev`.

No emulador Android, a versão debug usa `http://10.0.2.2:3000`:

```bash
./gradlew installDebug
```

Em celular conectado por USB, habilite a depuração e use:

```bash
adb reverse tcp:3000 tcp:3000
./gradlew installDebug -PapiBaseUrl=http://127.0.0.1:3000
```

Repita o `adb reverse` ao reconectar o celular. Para outro servidor, informe `-PapiBaseUrl=https://seu-servidor`. HTTP sem TLS é permitido somente na variante debug. A versão release precisa do endereço HTTPS real da API; o endereço `.example` é apenas um marcador, não um serviço publicado.

## Cadastro e acesso

Escolha doador ou ONG. Os dois exigem nome, e-mail, senha (8 a 128 caracteres) e confirmação idêntica. ONG também exige CNPJ válido e único, aceitando o formato numérico ou alfanumérico. Doadores não precisam informar CPF neste cadastro.

O cadastro grava a conta na API. Após a mensagem de sucesso, volte ao login, selecione o mesmo perfil e entre com o e-mail e a senha cadastrados. A área inicial mostra o nome retornado pelo banco; o perfil de doador mostra seu e-mail e o painel de ONG mostra e-mail e CNPJ.

Os acessos de demonstração foram removidos. A sessão fica apenas em memória no app e é revogada ao sair; ao reabrir o app é preciso fazer login novamente. “Lembrar e-mail” salva somente o endereço de e-mail, nunca senha ou token. Recuperação de senha ainda não está implementada. Campanhas, mapa e publicações ainda são telas locais de demonstração, fora desta integração de autenticação.

## Testes

```bash
./gradlew testDebugUnitTest assembleDebug
```

`AuthFlowTest` testa cadastro, mensagens de validação, senha incorreta, login e saída nos dois perfis. Ele só roda com `authE2E=true` e deve usar o banco isolado `sosplus_auth_test`, conforme README do servidor. Não aponte esse teste para o banco principal.

```bash
adb reverse tcp:3000 tcp:13001
./gradlew connectedDebugAndroidTest -PapiBaseUrl=http://127.0.0.1:3000 \
  -Pandroid.testInstrumentationRunnerArguments.authE2E=true \
  -Pandroid.testInstrumentationRunnerArguments.class=br.com.sosplus.AuthFlowTest
```

Depois do teste, restaure `adb reverse tcp:3000 tcp:3000` para voltar ao banco local principal.
