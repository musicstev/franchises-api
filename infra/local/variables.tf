variable "project_name" {
  description = "Prefijo de todos los recursos. Distinto al de docker-compose para que ambos puedan convivir sin colisión de nombres."
  type        = string
  default     = "franchises-tf"
}

variable "mongo_version" {
  description = "Tag de la imagen oficial de MongoDB."
  type        = string
  default     = "7"
}

variable "mongo_port" {
  description = "Puerto del host. Cámbialo (p. ej. 27018) si el stack de docker-compose ya ocupa el 27017."
  type        = number
  default     = 27017
}

variable "database_name" {
  description = "Base de datos que consume el API."
  type        = string
  default     = "franchises"
}

variable "docker_host" {
  description = "Socket del daemon Docker. Colima/Rancher Desktop: unix:///Users/<usuario>/.colima/default/docker.sock"
  type        = string
  default     = "unix:///var/run/docker.sock"
}
