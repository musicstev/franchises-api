# Contrato con la aplicación: este valor se inyecta tal cual en MONGO_URI.
output "mongo_uri" {
  description = "Cadena de conexión lista para el API."
  value       = "mongodb://localhost:${var.mongo_port}/${var.database_name}"
}

output "container_name" {
  description = "Nombre del contenedor creado."
  value       = docker_container.mongo.name
}

output "network_name" {
  description = "Red Docker creada, por si se quiere adjuntar el contenedor del API."
  value       = docker_network.this.name
}
