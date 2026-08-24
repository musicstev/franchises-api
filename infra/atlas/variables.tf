variable "atlas_public_key" {
  description = "API public key de MongoDB Atlas."
  type        = string
  sensitive   = true
}

variable "atlas_private_key" {
  description = "API private key de MongoDB Atlas."
  type        = string
  sensitive   = true
}

variable "atlas_org_id" {
  description = "ID de la organización en Atlas."
  type        = string
}

variable "project_name" {
  type    = string
  default = "franchises-api"
}

variable "cluster_name" {
  type    = string
  default = "franchises"
}

variable "cloud_provider" {
  description = "Proveedor que respalda el clúster compartido."
  type        = string
  default     = "AWS"
}

variable "region" {
  type    = string
  default = "US_EAST_1"
}

variable "instance_size" {
  description = "M0 es la capa gratuita."
  type        = string
  default     = "M0"
}

variable "database_name" {
  type    = string
  default = "franchises"
}

variable "db_username" {
  type    = string
  default = "franchises_app"
}

variable "db_password" {
  description = "Contraseña del usuario de aplicación. Pásala por TF_VAR_db_password o terraform.tfvars, nunca en el código."
  type        = string
  sensitive   = true
}

variable "allowed_cidr_blocks" {
  description = "Rangos IP autorizados. 0.0.0.0/0 solo para demo; en producción restringir."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}
