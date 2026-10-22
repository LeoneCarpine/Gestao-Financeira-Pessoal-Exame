# Requer JDK 17+ e dependencias do pom.xml ja baixadas pelo Maven/VS Code.
param([string]$CheckClass = 'app.controller.UsuarioControllerCadastroCheck')
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$checkDir = Join-Path $projectDir 'target/cadastro-check'
$classesDir = Join-Path $checkDir 'classes'
$libsDir = Join-Path $checkDir 'libs'
New-Item -ItemType Directory -Force -Path "$classesDir/META-INF", $libsDir | Out-Null
$repositoryDir = Join-Path ([Environment]::GetFolderPath('UserProfile')) '.m2/repository'
# Apenas dependencias de execucao, sem plugins de compilacao do Maven.
$groups = @('com/fasterxml/classmate', 'com/h2database/h2', 'com/sun/istack',
    'io/smallrye/jandex', 'jakarta', 'net/bytebuddy', 'org/antlr/antlr4-runtime',
    'org/eclipse/angus', 'org/glassfish/jaxb', 'org/hibernate', 'org/jboss/logging')
$jars = foreach ($group in $groups) {
    Get-ChildItem -LiteralPath (Join-Path $repositoryDir $group) -Filter '*.jar' -Recurse
}
foreach ($jar in $jars) { Copy-Item -LiteralPath $jar.FullName -Destination $libsDir -Force }
$classpath = ($jars | ForEach-Object { Join-Path $libsDir $_.Name }) -join ';'
$sources = (Get-ChildItem "$projectDir/src/main/java", "$projectDir/src/test/java" -Filter '*.java' -Recurse).FullName
& javac -encoding UTF-8 --release 17 -cp $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao.' }
$config = Get-Content "$projectDir/src/main/resources/META-INF/persistence.xml" -Raw
$config = $config.Replace('jdbc:h2:./financeiro', 'jdbc:h2:mem:cadastro_check;DB_CLOSE_DELAY=-1')
$config = $config.Replace('name="hibernate.show_sql" value="true"', 'name="hibernate.show_sql" value="false"')
[IO.File]::WriteAllText("$classesDir/META-INF/persistence.xml", $config)
& java -cp "$classesDir;$classpath" $CheckClass
if ($LASTEXITCODE -ne 0) { throw 'Falha nas verificacoes de cadastro.' }
