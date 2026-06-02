import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.util.Files
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

// Mirrors com.github.jk1.license.render.TextReportRenderer but writes the report to a constructor-
// supplied directory instead of `project.licenseReport.outputDir`. This is what lets us run the
// report once per Android flavor (foss / store) with separate output directories — the stock
// renderer hardcodes `project.licenseReport`, so every instance would clobber the same path.
class PerFlavorTextReportRenderer(private val outDir: File) : ReportRenderer {
    override fun render(data: ProjectData) {
        outDir.mkdirs()
        val output = File(outDir, "THIRD-PARTY-NOTICES.txt")
        val versionStr = data.project.version.toString()
        val versionPart = if (versionStr != "unspecified") versionStr else ""
        val sb = StringBuilder()
        sb.append("\nDependency License Report for ${data.project.name} $versionPart\n\n")
        var counter = 0
        for (module in data.allDependencies) {
            counter++
            sb.append("$counter.")
            if (!module.group.isNullOrEmpty()) sb.append(" Group: ${module.group} ")
            if (!module.name.isNullOrEmpty()) sb.append(" Name: ${module.name} ")
            if (!module.version.isNullOrEmpty()) sb.append(" Version: ${module.version}\n\n")

            if (module.poms.isEmpty() && module.manifests.isEmpty()) {
                sb.append("No license information found\n\n")
                continue
            }

            var projectUrlDone = false
            if (module.manifests.isNotEmpty() && module.poms.isNotEmpty()) {
                val manifest = module.manifests.first()
                val pom = module.poms.first()
                if (!manifest.url.isNullOrEmpty() && !pom.projectUrl.isNullOrEmpty() && manifest.url == pom.projectUrl) {
                    sb.append("Project URL: ${manifest.url}\n\n")
                    projectUrlDone = true
                }
            }
            if (module.manifests.isNotEmpty()) {
                val manifest = module.manifests.first()
                if (!manifest.url.isNullOrEmpty() && !projectUrlDone) {
                    sb.append("Manifest Project URL: ${manifest.url}\n\n")
                }
                if (!manifest.license.isNullOrEmpty()) {
                    when {
                        Files.maybeLicenseUrl(manifest.licenseUrl) ->
                            sb.append("Manifest license URL: ${manifest.licenseUrl}\n\n")
                        manifest.hasPackagedLicense ->
                            sb.append("Packaged License File: ${manifest.license}\n\n")
                        else ->
                            sb.append("Manifest License: ${manifest.license} (Not packaged)\n\n")
                    }
                }
            }
            if (module.poms.isNotEmpty()) {
                val pom = module.poms.first()
                if (!pom.projectUrl.isNullOrEmpty() && !projectUrlDone) {
                    sb.append("POM Project URL: ${pom.projectUrl}\n\n")
                }
                for (license in pom.licenses) {
                    sb.append("POM License: ${license.name}")
                    if (!license.url.isNullOrEmpty()) {
                        if (Files.maybeLicenseUrl(license.url)) {
                            sb.append(" - ${license.url}\n\n")
                        } else {
                            sb.append("License: ${license.url}\n\n")
                        }
                    }
                }
            }
            if (module.licenseFiles.isNotEmpty() && module.licenseFiles.first().fileDetails.isNotEmpty()) {
                sb.append("Embedded license: \n\n")
                for (details in module.licenseFiles.first().fileDetails) {
                    sb.append("                    ****************************************                    \n\n")
                    sb.append(File(outDir, details.file).readText())
                    sb.append("\n")
                }
            }
            sb.append("--------------------------------------------------------------------------------\n\n")
        }
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC' yyyy").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sb.append("\nThis report was generated at ${formatter.format(Date())}.\n\n")
        output.writeText(sb.toString())
    }
}
