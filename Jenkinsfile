@Library('utils') _
import com.spotinst.GlobalVars

def pod = renderPod(
    gradle:        true,
    gradleVersion: '8.5',
    kaniko:        true,
    kubectl:       true,
    oceancdcli:    true,
    yq:            true,
)

def svcName = currentBuild.rawBuild.project.parent.displayName
def clouds = ['aws']
def defaultBranch = 'main'
def prodDeployType = 'k8s'
def additionalDeployments = []

javaMCPipeline(
    svcName,
    pod,
    clouds,
    defaultBranch
)