String basePath = 'Spicegate-Hardware-GUI'
String repo = 'git://git.host.prod.eng.bos.redhat.com/spicegate.git'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}


def RHEL = ['7.x','8.x']
def ARCH = ['x86_64','ppc-64']
def JSLAVE_x86 = "RHCERTQE-RHEL_x86_64"
def JSLAVE_ppc = "RHCERTQE-RHEL_ppc"



for (version in RHEL)
{
    for (arch in ARCH) {
        def uuid = UUID.randomUUID().toString()
        job("$basePath/Hardware-spicegate-gui-$version-$arch") {

            if ( arch == 'x86_64' ){

                label(JSLAVE_x86)
            }else{
                label(JSLAVE_ppc)
            }

            scm {
                git repo
            }
            triggers {
                scm 'H/5 * * * *'
            }
            steps {
                shell 'scp war file; restart...'
            }

            configure { Node project ->
                project / triggers /'com.redhat.jenkins.plugins.ci.CIBuildTrigger'{
                    spec {}
                    noSquash false
                    providerData(class: "com.redhat.jenkins.plugins.ci.provider.data.ActiveMQSubscriberProviderData")
                            {
                                name ("Red Hat UMB")
                                overrides
                                        {
                                            topic("Consumer.rh-jenkins-ci-plugin."+uuid+".VirtualTopic.eng.brew.build.complete")
                                        }
                                selector("name = 'redhat-certification' AND release LIKE  '%el8'")
                                checks {}
                            }
                }
            }
        }
    }
}