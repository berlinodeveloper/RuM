package util;

public enum PageType {
	START("pages/common/StartPage.fxml"),
	DISCOVERY("pages/discovery/DiscoveryPage.fxml"),
	CONFORMANCE("pages/conformance/ConformancePage.fxml"),
	GENERATION("pages/generation/GenerationPage.fxml"),
	EDITOR("pages/editor/EditorPage.fxml"),
	MONITORING("pages/monitoring/MonitoringPage.fxml");
	
	private final String pathToFxml;
	
	private PageType(String pathToFxml) {
		this.pathToFxml = pathToFxml;
	}
	
	public String getPathToFxml() {
		return pathToFxml;
	}
}
