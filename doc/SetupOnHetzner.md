# Setup 
## Infrastructure on Hetzner / Aws

For a setup on hetzner / aws we use terraform.

```
resource "aws_s3_bucket" "backup" {
  bucket = "backup"
  acl    = "private"

  versioning {
    enabled = false
  }
  tags = {
    name        = "backup"
    Description = "bucket for backups in stage: ${var.stage}"
  }
}

resource "hcloud_server" "jira_09_2021" {
  name        = "the name"
  image       = "ubuntu-20.04"
  server_type = "cx31"
  location    = "fsn1"
  ssh_keys    = ...

  lifecycle {
    ignore_changes        = [ssh_keys]
  }
}

resource "aws_route53_record" "v4_neu" {
  zone_id = the_dns_zone
  name    = "jira-neu"
  type    = "A"
  ttl     = "300"
  records = [hcloud_server.jira_09_2021.ipv4_address]
}

output "ipv4" {
  value = hcloud_server.jira_09_2021.ipv4_address
}

```

## k8s minicluster

For k8s installation we use our [dda-k8s-crate](https://github.com/DomainDrivenArchitecture/dda-k8s-crate) with the following configuation:


```
{:user :k8s
 :k8s {:external-ip "ip-from-above"}
 :cert-manager :letsencrypt-prod-issuer
 :persistent-dirs ["jira", "postgres"]
 }
```

## kubectl apply c4k-jira

The last step for applying the jira deployment is

```
c4k-jira config.edn auth.edn | kubectl apply -f -
```

with the following config.edn:

```
{:fqdn "the-fqdn-from aws_route53_record.v4_neu"
 :jira-data-volume-path "/var/jira"                 ;; Volume was configured at dda-k8s-crate, results in a PersistentVolume definition.
 :postgres-data-volume-path "/var/postgres"         ;; Volume was configured at dda-k8s-crate, results in a PersistentVolume definition.
 :restic-repository "s3:s3.amazonaws.com/your-bucket/your-folder"}
```
