# SignPath-Einrichtung nach Annahme des Projekts

Diese Schritte werden erst ausgeführt, nachdem SignPath Foundation die
Open-Source-Bewerbung angenommen und die Organisation eingerichtet hat.

## Angaben für die Bewerbung

- Projekt: Blasrohr-Wettkampf-Manager
- Repository: `https://github.com/Manadhion/wettkampf_software`
- Lizenz: MIT
- Download/Release: `https://github.com/Manadhion/wettkampf_software/releases/latest`
- Code signing policy: `CODE-SIGNING-POLICY.md`
- Datenschutzinformation: `PRIVACY.md`
- Build: `.github/workflows/windows-installer.yml`

## Nach der Annahme

1. SignPath-GitHub-App installieren und ausschließlich für dieses Repository
   freigeben.
2. In SignPath das Repository als vertrauenswürdiges GitHub-Buildsystem mit dem
   Projekt verbinden.
3. Eine Artifact Configuration für
   `Blasrohr-Wettkampf-Manager-<version>.exe` anlegen bzw. von SignPath anlegen
   lassen.
4. Eine Release-Signing-Policy mit Herkunftsprüfung und manueller Freigabe
   konfigurieren.
5. Den von SignPath erzeugten API-Token in GitHub als Repository-Secret
   `SIGNPATH_API_TOKEN` speichern. Der Wert gehört nie in eine Datei oder einen
   Chat.
6. Von SignPath bereitgestellte Werte für Organisation, Projekt, Policy und
   Artifact Configuration in den Workflow eintragen.

Der dafür vorgesehene Workflow-Schritt lautet anschließend sinngemäß:

```yaml
- name: Signaturanfrage an SignPath senden
  uses: signpath/github-action-submit-signing-request@v2
  with:
    api-token: ${{ secrets.SIGNPATH_API_TOKEN }}
    organization-id: <von SignPath>
    project-slug: <von SignPath>
    signing-policy-slug: <von SignPath>
    github-artifact-id: ${{ steps.upload-unsigned-artifact.outputs.artifact-id }}
    wait-for-completion: true
    output-artifact-directory: dist/signed
```

Die Platzhalter dürfen erst nach Erhalt der echten SignPath-Konfiguration
ersetzt werden. Der signierte Installer sollte als neue Version veröffentlicht
werden; ein bereits veröffentlichtes unsigniertes Artefakt wird nicht heimlich
unter demselben Versionsnamen ausgetauscht.
