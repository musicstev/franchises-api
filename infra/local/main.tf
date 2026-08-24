# Aprovisiona la persistencia del API: imagen, red, volumen y contenedor de MongoDB.
# Es una alternativa declarativa a docker-compose, no un reemplazo: ambos pueden
# coexistir en el repositorio, pero no ejecutarse a la vez sobre el mismo puerto.

resource "docker_image" "mongo" {
  name         = "mongo:${var.mongo_version}"
  keep_locally = true # no borrar la imagen al hacer destroy
}

resource "docker_network" "this" {
  name = "${var.project_name}-net"
}

# Volumen dedicado: los datos sobreviven a la recreación del contenedor y son
# independientes de los volúmenes creados por docker-compose.
resource "docker_volume" "mongo_data" {
  name = "${var.project_name}-mongo-data"
}

resource "docker_container" "mongo" {
  name    = "${var.project_name}-mongodb"
  image   = docker_image.mongo.image_id
  restart = "unless-stopped"

  ports {
    internal = 27017
    external = var.mongo_port
  }

  volumes {
    volume_name    = docker_volume.mongo_data.name
    container_path = "/data/db"
  }

  networks_advanced {
    name = docker_network.this.name
  }

  healthcheck {
    test     = ["CMD", "mongosh", "--quiet", "--eval", "db.adminCommand('ping')"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
  }
}
