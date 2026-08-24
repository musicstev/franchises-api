resource "mongodbatlas_project" "this" {
  name   = var.project_name
  org_id = var.atlas_org_id
}

# Clúster M0: capa gratuita permanente, suficiente para la prueba técnica.
resource "mongodbatlas_advanced_cluster" "this" {
  project_id   = mongodbatlas_project.this.id
  name         = var.cluster_name
  cluster_type = "REPLICASET"

  replication_specs {
    region_configs {
      provider_name         = "TENANT" # M0/M2/M5 son clústeres compartidos
      backing_provider_name = var.cloud_provider
      region_name           = var.region
      priority              = 7

      electable_specs {
        instance_size = var.instance_size
      }
    }
  }
}

resource "mongodbatlas_database_user" "app" {
  project_id         = mongodbatlas_project.this.id
  username           = var.db_username
  password           = var.db_password
  auth_database_name = "admin"

  # Principio de mínimo privilegio: solo lectura/escritura sobre su propia base.
  roles {
    role_name     = "readWrite"
    database_name = var.database_name
  }
}

resource "mongodbatlas_project_ip_access_list" "allowed" {
  for_each = toset(var.allowed_cidr_blocks)

  project_id = mongodbatlas_project.this.id
  cidr_block = each.value
  comment    = "Gestionado por Terraform"
}
