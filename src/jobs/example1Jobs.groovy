String basePath = 'Spicegate-Hardware-GUI'
String repo = 'git://git.host.prod.eng.bos.redhat.com/spicegate.git'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}


def RHEL = ['7.x','8.x']
def ARCH = ['x86_64','ppc-64']


for (version in RHEL)
{
    for (arch in ARCH) {
        job("$basePath/Hardware-spicegate-gui-$version-$arch") {
            label('node1')
            scm {
                github repo
            }
            triggers {
                scm 'H/5 * * * *'
            }
            steps {
                shell 'scp war file; restart...'
            }
        }
    }
}
