{{- /*
Determine if DAMAP and Keycloak share the same host.
If yes, prefix Keycloak paths with /auth to avoid collisions.
*/}}
{{- define "keycloakPath" }}
  {{- $sameHost := eq .Values.keycloak.hostname .Values.damap.hostname }}
  {{- if $sameHost }}
    /auth
  {{- else }}
    /
  {{- end }}
{{- end }}
