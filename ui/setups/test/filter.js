var setup = {
	controllers: [
		{
			name: 'defaultLogger',
			logActionConsole: false,
			logEventConsole: false
		},
		{ name: 'canvasHoverController' },
		{ name: 'canvasFilterController' },
		{
			name: 'filterController',
			devMode: false,
			configuration: 'default.json'
		},
		{
			name: 'experimentController',
			taskTextButtonTime: 1,
			taskTime: 1,
			stepOrder: [0, 10, 20, 30, 40, 45, 50, 60, 70, 80, 90, 100],
			steps: [
				{
					number: 0,
					text: [
						'Willkommen zur Evaluation RedPill!',
						'',
						'',
						'Im Folgenden werden Sie durch ein Tutorial geleitet, das Sie auf 5 Aufgaben vorbereitet, die Sie im Anschluss lösen werden.',
						'Wenn Sie mit einer fertig sind, klicken Sie auf den Button "Weiter" oben rechts und bestätigen Sie diese Aktion.'
					]
				},
				{
					number: 10,
					text: [
						'Tutorial: Aufgabe 1 - Selektion',
						'',
						'Ein leeres Suchfeld selektiert automatisch alle vorhandenen Elemente der Visualisierung. Der erste Container wendet seine Transformation "visible" also auf alle Elemente an.',
						'Die erste Ebene soll die Klasse "InCallScreen" aus dem Paket "com.android.phone" selektieren. Tragen Sie den Klassennamen in das Suchfeld ein, bis unter den Vorschlägen die Klasse aus dem richtigen Paket erscheint ("com.android.phone.InCallScreen").',
						'Wählen Sie nun per Mausklick (oder mit den Pfeiltasten und Enter) den Vorschlag aus.',
						'Fügen Sie dem ersten Container nun eine weitere Ebene hinzu. Nutzen Sie dazu den "add new layer"-Button.',
						'Selektieren Sie in dieser Ebene nun die Klasse "CallCard" aus dem Paket "com.android.phone" um letztendlich nur diese beiden Klassen anzuzeigen.'
					]
				},
				{
					number: 20,
					text: [
						'Tutorial: Aufgabe 2 - Transformation',
						'',
						'',
						'Fügen Sie einen weiteren Container des Typs "transparent" hinzu. Nutzen Sie dazu den "add new container"-Button.',
						'Selektieren Sie in der ersten Ebene dieses neuen Containers die Klasse "CallCard" aus dem Paket "com.android.phone".',
						'Fügen Sie dann einen weiteren Container des Typs "selected" hinzu, dessen erste Ebene das Attribut "mSettings" der Klasse "com.android.phone.InCallScreen" selektiert.'
					]
				},
				{
					number: 30,
					text: [
						'Tutorial: Aufgabe 3 - Manipulation',
						'',
						'',
						'Blenden Sie nun alle Elemente ein, die mit der Klasse "InCallScreen" in Beziehung stehen. Schalten Sie dazu im ersten Container die Option "relations" ein.',
						'Testen Sie danach, was passiert wenn sie im zweiten Container die Option "invert" einschalten.'
					]
				},
				{
					number: 40,
					text: [
						'Tutorial: Aufgabe 4 - Relationen',
						'',
						'Setzen Sie die Filterkonfiguration zurück. Nutzen Sie dazu den "reset configuration"-Button.',
						'Blenden Sie nun nur die beiden Klassen "OtaStartupReceiver" und "OtaUtils" aus dem Paket "com.android.phone" ein.',
						'Um nun die Relationen zwischen diesen Klassen anzuzeigen, fügen Sie einen neuen Container des Typs "connected" hinzu.',
						'Er selektiert durch das leere Suchfeld alle Elemente der beiden Klassen und zeigt ihre Relationen an.',
						'Sie können mit dem "reset view"-Button die optimale Zoomstufe einstellen, um die Visualisierung genauer zu betrachten.'
					]
				},
				{
					number: 45,
					text: [
						'Tutorial abgeschlossen',
						'',
						'',
						'Sie haben das Tutorial nun abgeschlossen. Füllen Sie nun den "Fragebogen 2" aus.',
						'Die folgenden Aufgaben stellen reale Anwendungsfälle der Softwarevisualisierung dar.',
						'',
						'Beantworten Sie nach jeder Aufgabe die entsprechende Frage im "Frageboigen 3".'
					]
				},
				{
					number: 50,
					text: [
						'Aufgabe 5',
						'',
						'',
						'Zeigen sie zwei Klassen an, und nur von einer zusätzlich die in Beziehung stehenden Elemente.'
					]
				},
				{
					number: 60,
					text: [
						'Aufgabe 6',
						'',
						'',
						'Mit welchen anderen Klassen steht die Klasse "com.android.phone.ADNList" in Beziehung?'
					]
				},
				{
					number: 70,
					text: [
						'Aufgabe 7',
						'',
						'',
						'Zeigen Sie nur die Klasse "com.android.phone.InCallScreen" an und blenden sie alle Elemente transparent ein, die mit ihr in Beziehung stehen.'
					]
				},

				{
					number: 80,
					text: [
						'Aufgabe 8',
						'',
						'',
						'Transformieren Sie die Visualisierung so, dass alle Elemente der Visualisierung transparent dargestellt werden, das Attribut "com.android.phone.InCallScreen.otaUtils" und die in Beziehung stehenden Elemente sollen jedoch undurchsichtig bleiben.'
					]
				},

				{
					number: 90,
					text: [
						'Aufgabe 9',
						'',
						'',
						'Markieren Sie alle Elemente der Klasse "com.android.phone.CallNotifier" als selected, die in Beziehung zur Klasse "com.android.phone.PhoneUtils" stehen.'
					]
				},
				{
					number: 100,
					text: [
						'Aufgaben abgeschlossen',
						'',
						'',
						'Sie haben alle Aufgaben abgeschlossen und fahren nun mit dem Gesamtfeedback fort.'
					]
				}
			]
		}
	],
	uis: [
		{
			name: 'UI0',
			navigation: { type: 'examine' },

			area: {
				name: 'top',
				orientation: 'horizontal',
				first: {
					size: '125px',
					collapsible: false,
					controllers: [{ name: 'experimentController' }]
				},
				second: {
					name: 'bottom',
					size: '90%',
					collapsible: false,
					area: {
						name: 'left',
						orientation: 'vertical',
						first: {
							size: '25%',
							min: '250',
							collapsible: true,
							expanders: [
								{
									name: 'filter',
									title: 'Filter',
									controllers: [{ name: 'filterController' }]
								}
							]
						},
						second: {
							name: 'right',
							size: '75%',
							min: '200',
							collapsible: false,
							canvas: {},
							controllers: [
								{ name: 'defaultLogger' },
								{ name: 'canvasHoverController' },
								{ name: 'canvasFilterController' }
							]
						}
					}
				}
			}
		}
	]
};
