<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">


        <script src="https://cdn.jsdelivr.net/npm/vue"></script>
        <script src="https://unpkg.com/axios/dist/axios.min.js"></script>


        <style>
            [v-cloak] > * { display:none; }
            [v-cloak]::before { content: "loading..."; }
        </style>

        <div id="vue-app">
            <div v-cloak>
                <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
                    <div class="alert alert-error" v-show="errorMessage">
                        <span class="${properties.kcFeedbackErrorIcon!}"></span>
                        <span class="kc-feedback-text">{{ errorMessage }}</span>
                    </div>

                    <div class="${properties.kcFormGroupClass!}">
                        <div class="${properties.kcLabelWrapperClass!}">
                            <ul class="nav nav-pills nav-justified">
                                <li role="presentation" v-bind:class="{ active: usernameOrPhone }" v-on:click="usernameOrPhone = true"><a href="#"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></a></li>
                                <li role="presentation" v-bind:class="{ active: !usernameOrPhone }" v-on:click="usernameOrPhone = false"><a href="#">${msg("phone")}</a></li>
                            </ul>
                        </div>
                    </div>

                    <div v-if="usernameOrPhone">
                        <div class="${properties.kcFormGroupClass!}">
                            <div class="${properties.kcLabelWrapperClass!}">
                                <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
                            </div>
                            <div class="${properties.kcInputWrapperClass!}">
                                <input type="text" id="username" name="username" class="${properties.kcInputClass!}" autofocus/>
                            </div>
                        </div>
                    </div>

                    <div v-show="!usernameOrPhone">
                        <div class="${properties.kcFormGroupClass!}">
                            <div class="${properties.kcLabelWrapperClass!}">
                                <label for="phoneNumber" class="${properties.kcLabelClass!}">${msg("phoneNumber")}</label>
                            </div>
                            <div class="${properties.kcInputWrapperClass!}">
                                <input type="text" id="phoneNumber" name="phoneNumber" v-model="phoneNumber" class="${properties.kcInputClass!}" autofocus/>
                            </div>
                        </div>

                        <div class="${properties.kcFormGroupClass!}">
                            <div class="${properties.kcLabelWrapperClass!}">
                                <label for="code" class="${properties.kcLabelClass!}">${msg("verificationCode")}</label>
                            </div>
                            <div class="col-xs-9 col-sm-9 col-md-9 col-lg-9">
                                <input type="text" id="code" name="code" class="${properties.kcInputClass!}" autofocus/>
                            </div>
                            <div class="col-xs-3 col-sm-3 col-md-3 col-lg-3">
                                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                       type="button" v-model="sendButtonText" :disabled='sendButtonText !== initsendButtonText' v-on:click="sendVerificationCode()"/>
                            </div>
                        </div>


                    </div>

                    <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                        <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                            <div class="${properties.kcFormOptionsWrapperClass!}">
                                <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                            </div>
                        </div>

                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <script type="text/javascript">
            var app = new Vue({
                el: '#vue-app',
                data: {
                    errorMessage: '',
                    freezeSendCodeSeconds: 0,
                    usernameOrPhone: true,
                    phoneNumber: '',
                    sendButtonText: '${msg("sendVerificationCode")}',
                    initsendButtonText: '${msg("sendVerificationCode")}',
                    disableSend: function(seconds) {
                        if (seconds <= 0) {
                            app.sendButtonText = app.initsendButtonText;
                            app.freezeSendCodeSeconds = 0;
                        } else {
                            app.sendButtonText = String(seconds);
                            setTimeout(function() {
                                app.disableSend(seconds - 1);
                            }, 1000);
                        }
                    },
                    sendVerificationCode: function() {

                        const phoneNumber = document.getElementById('phoneNumber').value.trim();
                        if (!phoneNumber) {
                            this.errorMessage = '${msg("requirePhoneNumber")}';
                            document.getElementById('phoneNumber').focus();
                            return;
                        }
                        if (this.sendButtonText !== this.initsendButtonText) {
                            return;
                        }
                        this.disableSend(60);
                        const params = new URLSearchParams();
                        params.append('phoneNumber', this.phoneNumber);
                        params.append('kind', '${verificationCodeKind}');

                        axios
                            .post(window.location.origin + '/auth/realms/${realm.name}/sms/authentication-code', params)
                            .then(res => (console.log(res.status)));
                    }
                }
            });
        </script>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>