var Application = (function(application) {
	application.Views.SettingsView = Backbone.View.extend({
		el : $("#main_content"),
		url : "/backend/settings",
		initialize : function() {
			this.template = $.getTemplate("settings.html");
		},
		render : function() {
			var that = this;
			$.getJSON(this.url, function(response) {
				var renderedContent = Mustache.to_html(that.template, {
					settings : response,
					title : $.i18n.prop("msg.settings.title"),
					settingsServerName : $.i18n.prop("msg.settings.serverName"),
					settingsPrependPodcastItem : $.i18n.prop("msg.settings.prependPodcastItem"),
					settingsEnableIcecastDirectory : $.i18n.prop("msg.settings.enableIcecastDirectory"),
					cancel : $.i18n.prop("msg.cancel"),
					save : $.i18n.prop("msg.save")
				});
				that.$el.html(renderedContent);
			});
		},
		events : {
			"click #btnSettingsSave" : "onSettingsSave",
			"click #btnSettingsCancel" : "onSettingsCancel"
		},
		onSettingsSave : function() {
			new Application.Models.Settings().save({
				"serverName" : $("#settingsServerName").val().trim(),
				"prependPodcastItem" : $("#chkPrependPodcastItem").is(':checked') ? "true" : "false",
				"enableIcecastDirectory" : $("#chkEnableIcecastDirectory").is(':checked') ? "true" : "false"
			}, {
				success : function() {
					$("#messagebox").message({
						text : $.i18n.prop("msg.settings.saved"),
						type : "success"
					});
				},
				error : function(model, response) {
					$("#messagebox").message({
						text : response.responseText || response.statusText,
						type : "danger"
					});
				}
			});
		},
		onSettingsCancel : function() {
			this.render();
		}
	});
	return application;
}(Application));