import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.render.TextReportRenderer

apply(plugin = "com.github.jk1.dependency-license-report")

configure<LicenseReportExtension> {
    excludeOwnGroup = true
    renderers = arrayOf(TextReportRenderer())
    excludes = arrayOf("CaveCraft.*")
}
