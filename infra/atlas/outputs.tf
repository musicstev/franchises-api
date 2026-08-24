# Inyecta las credenciales en la cadena SRV para obtener un MONGO_URI utilizable.
output "mongo_uri" {
  description = "Cadena de conexión completa para MONGO_URI."
  value = replace(
    mongodbatlas_advanced_cluster.this.connection_strings[0].standard_srv,
    "mongodb+srv://",
    "mongodb+srv://${var.db_username}:${var.db_password}@"
  )
  sensitive = true
}

output "cluster_host" {
  description = "Host del clúster, sin credenciales (seguro de mostrar en logs)."
  value       = mongodbatlas_advanced_cluster.this.connection_strings[0].standard_srv
}
