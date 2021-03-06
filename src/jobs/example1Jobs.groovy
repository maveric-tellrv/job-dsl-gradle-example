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

            parameters {
                stringParam('SUT_HOST','','Add a sut host to test')
                stringParam('rhcertuser','linux-certs-dell','define certification user')
                stringParam('rhcertpass','redhat','certification user password')
                stringParam('ansible_user_LTS','root','LTS user to logn machine')
                stringParam('ansible_user_LTS_password','redhat','Password for SUT machine')
                stringParam('pass','cert-qe123*','SUT password')
                stringParam('rhel_version','7.4','Rhel version for certificate')
                stringParam('platform',arch,'Certifcate architecture')
                stringParam('vendor','Dell (ID: 1)','vendor information')
                booleanParam('clean_rhcert_store', true,'Clean all the cert test')
                choiceParam('Test', ['hwcert_certificate', 'All', 'hardware-profiler'],'Select the test to Run')
                choiceParam('Packages',['rhcert_backend_hardware','rhcertwebui_backend'],'packages to install')
                choiceParam('test_env',['hydrastage','hydraqa','qa','stage'],'test env')
                choiceParam('Install_OR_uninstall',['install','uninstall','TestRunOnly'],'Install latest rhcert packages')
            }

            if ( arch == 'x86_64' ){

                label(JSLAVE_x86)
            }else{
                label(JSLAVE_ppc)
            }

            scm {
                git repo
            }
            triggers {
                scm 'H H(0-12) * * *'
            }
            steps {
                shell readFileFromWorkspace('src/scripts/hardware_gui_conf.sh')
            }
            publishers {
                archiveJunit('reports/*.xml')
                extendedEmail {
                    recipientList('rhcert-reports@redhat.com')
                    defaultSubject('''Spicegate Hardware QA Automation  -${BUILD_NUMBER}+${SUT_HOST}''')
                    defaultContent('''http://certqe-jenkins.gsslab.pnq.redhat.com/job/Spicegate_Hardware_HYDRAQA_ENV/${BUILD_NUMBER}/allure/''')
                    contentType('text/html')
                    triggers {
                        always()
                    }
                }
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