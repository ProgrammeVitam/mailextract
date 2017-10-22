package fr.gouv.vitam.tools.mailextract.lib.core;

public class StoreExtractorOptions {
	public boolean keepOnlyDeepEmptyFolders;
	public boolean dropEmptyFolders;
	public boolean warningMsgProblem;
	public int namesLength;

	public StoreExtractorOptions() {
		keepOnlyDeepEmptyFolders = false;
		dropEmptyFolders = false;
		warningMsgProblem = false;
		namesLength = 12;
	}

	public StoreExtractorOptions(boolean keepOnlyDeepEmptyFolders, boolean dropEmptyFolders, boolean warningMsgProblem,
			int namesLength) {
		this.keepOnlyDeepEmptyFolders = keepOnlyDeepEmptyFolders;
		this.dropEmptyFolders = dropEmptyFolders;
		this.warningMsgProblem = warningMsgProblem;
		this.namesLength = namesLength;
	}
}
