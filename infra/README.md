# Infraestructura como código (Terraform)

Aprovisiona la persistencia del API de forma declarativa, como alternativa a
`docker-compose.yml`. El API solo conoce la variable `MONGO_URI`; no le importa qué
herramienta creó la base de datos detrás de ella.

## Variantes disponibles

| Carpeta | Qué crea | Requiere | Uso recomendado |
|---|---|---|---|
| [`local/`](local) | Un contenedor MongoDB vía el provider Docker | Solo Docker (Colima, Docker Desktop, Rancher Desktop) | Demostración reproducible por cualquiera que clone el repo |
| [`atlas/`](atlas) | Un clúster M0 (capa gratuita) en MongoDB Atlas | Cuenta de Atlas + API keys propias | Evidencia de aprovisionamiento en una nube real |

## Uso

```bash
cd infra/local        # o infra/atlas
terraform init         # descarga los providers
terraform plan          # muestra el diff, no aplica nada todavía
terraform apply
export MONGO_URI=$(terraform output -raw mongo_uri)
```

Luego arranca el API normalmente (`./mvnw spring-boot:run` o el jar/imagen) usando esa
`MONGO_URI`. Al terminar:

```bash
terraform destroy
```

## Importante

- **No es un reemplazo de `docker-compose.yml`** — es una vía alternativa. No ejecutes
  ambos a la vez sobre el mismo puerto 27017 (usa `-var mongo_port=27018` en `local/`
  si necesitas que convivan).
- `terraform.tfvars` y el *state* (`*.tfstate`) nunca se versionan — están en
  `.gitignore`. Cada carpeta trae un `terraform.tfvars.example` como plantilla.
- `terraform destroy` solo afecta lo que el propio Terraform creó; nunca toca los
  contenedores o volúmenes de `docker-compose`.
