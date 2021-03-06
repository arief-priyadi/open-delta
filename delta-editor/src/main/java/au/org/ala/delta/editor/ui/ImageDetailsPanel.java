/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.delta.editor.ui;

import au.org.ala.delta.editor.DeltaEditor;
import au.org.ala.delta.editor.model.EditorViewModel;
import au.org.ala.delta.editor.ui.dnd.SimpleTransferHandler;
import au.org.ala.delta.editor.ui.util.MessageDialogHelper;
import au.org.ala.delta.model.Illustratable;
import au.org.ala.delta.model.image.Image;
import au.org.ala.delta.model.image.ImageOverlay;
import au.org.ala.delta.model.image.ImageSettings;
import au.org.ala.delta.model.image.OverlayType;
import au.org.ala.delta.model.observer.AbstractDataSetObserver;
import au.org.ala.delta.model.observer.DeltaDataSetChangeEvent;
import au.org.ala.delta.model.observer.DeltaDataSetObserver;
import au.org.ala.delta.ui.image.AudioPlayer;
import au.org.ala.delta.ui.image.SupportedFileTypes;
import au.org.ala.delta.ui.rtf.RtfEditorPane;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Displays the details of images associated with an Item or Character.
 */
public class ImageDetailsPanel extends JPanel {
	
	private static final long serialVersionUID = -1973824161019895786L;
	
	private EditorViewModel _dataSet;
	
	/** The object that any images will be attached to */
	private Illustratable _illustratable;

	/** The currently selected image */
	private Image _selectedImage;
	
	/** Helper class for displaying messages */
	private MessageDialogHelper _messageHelper;
	
	private ActionMap _actions;
	private ResourceMap _resources;
	
	// UI components
	private ImageList imageList;
	private RtfEditorPane subjectTextPane;
	private RtfEditorPane developerNotesTextPane;
	private JButton btnDisplay;
	private JButton btnDelete;
	private JButton btnSettings;
	private JButton btnAdd;
	private JButton deleteSoundButton;
	private JButton playSoundButton;
	private JButton insertSoundButton;
	private JComboBox soundComboBox;

	
	public ImageDetailsPanel() {
		_actions = Application.getInstance().getContext().getActionMap(this);
		_resources = Application.getInstance().getContext().getResourceMap();
		_messageHelper = new MessageDialogHelper();
		createUI();
		addEventHandlers();
	}
	
	private void addEventHandlers() {
		
		btnDisplay.setAction(_actions.get("displayImage"));
		btnAdd.setAction(_actions.get("addImage"));
		btnDelete.setAction(_actions.get("deleteImage"));
		btnSettings.setAction(_actions.get("displayImageSettings"));
		imageList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				_selectedImage = (Image)imageList.getSelectedValue();
				_dataSet.setSelectedImage(_selectedImage);
				updateDisplay();
			}
		});
		imageList.setSelectionAction(_actions.get("displayImage"));
		imageList.setDragEnabled(true);
		imageList.setDropMode(DropMode.INSERT);
		imageList.setTransferHandler(new ImageTransferHandler());
		
		playSoundButton.setAction(_actions.get("playSound"));
		deleteSoundButton.setAction(_actions.get("deleteSound"));
		insertSoundButton.setAction(_actions.get("addSound"));
		FocusAdapter focusAdaptor = new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				if (e.getComponent() == subjectTextPane) {
					updateSubjectText();
				}
				else if (e.getComponent() == developerNotesTextPane) {
					updateDeveloperNotes();
				}
			}
		
		};
		soundComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSoundActions();
			}
		});
		subjectTextPane.addFocusListener(focusAdaptor);
		developerNotesTextPane.addFocusListener(focusAdaptor);
	}

	private void createUI() {
		JPanel panel = new JPanel();
		
		JPanel buttonPanel = new JPanel();
		
		JPanel panel_2 = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 262, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 265, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
						.addComponent(buttonPanel, 0, 0, Short.MAX_VALUE)
						.addComponent(panel_2, 0, 0, Short.MAX_VALUE))
					.addContainerGap())
		);
		
		btnSettings = new JButton();
		btnDisplay = new JButton();
		btnAdd = new JButton();
		btnDelete = new JButton();
		
		GroupLayout gl_buttonPanel = new GroupLayout(buttonPanel);
		gl_buttonPanel.setHorizontalGroup(
			gl_buttonPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_buttonPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_buttonPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(btnSettings, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnDisplay, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
						.addComponent(btnAdd, GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
						.addComponent(btnDelete, GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_buttonPanel.setVerticalGroup(
			gl_buttonPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_buttonPanel.createSequentialGroup()
					.addContainerGap(119, Short.MAX_VALUE)
					.addComponent(btnSettings)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnDisplay)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAdd)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnDelete)
					.addContainerGap())
		);
		buttonPanel.setLayout(gl_buttonPanel);
		
		JLabel lblSubjectText = new JLabel();
		lblSubjectText.setName("imageDetailsSubjectLabel");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		JLabel lblDevelopersNotes = new JLabel();
		lblDevelopersNotes.setName("imageDetailsDevelopersNotesLabel");
		
		JScrollPane scrollPane_2 = new JScrollPane();
		
		JPanel panel_3 = new JPanel();
		
		String title = _resources.getString("imageDetailsSoundTitle");
		
		panel_3.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
						.addComponent(lblDevelopersNotes)
						.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
						.addComponent(lblSubjectText))
					.addContainerGap())
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblSubjectText)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblDevelopersNotes)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		
		developerNotesTextPane = new RtfEditorPane();
		scrollPane_2.setViewportView(developerNotesTextPane);
		
		subjectTextPane = new RtfEditorPane();
		scrollPane_1.setViewportView(subjectTextPane);
		
		soundComboBox = new JComboBox();
		soundComboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				String text = "";
				ImageOverlay overlay = (ImageOverlay)value;
				if (overlay != null) {
					text = overlay.overlayText;
				}
				super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
				return this;
			}
		});
		
		deleteSoundButton = new JButton();
		
		playSoundButton = new JButton();
		
		insertSoundButton = new JButton();
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addComponent(soundComboBox, 0, 0, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(deleteSoundButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(playSoundButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(insertSoundButton)
					.addContainerGap())
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap(5, Short.MAX_VALUE)
					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
						.addComponent(soundComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(deleteSoundButton)
						.addComponent(playSoundButton)
						.addComponent(insertSoundButton)))
		);
		panel_3.setLayout(gl_panel_3);
	
		panel_2.setLayout(gl_panel_2);
		
		JLabel lblImageFiles = new JLabel();
		lblImageFiles.setName("imageDetailsImageFilesLabel");
		lblImageFiles.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblImageFiles)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblImageFiles)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		imageList = new ImageList();
	
		
		scrollPane.setViewportView(imageList);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);
	}
	
	/**
	 * Binds the supplied Illustratable to the user interface provided by this 
	 * class.
	 * @param target
	 */
	public void bind(EditorViewModel model, Illustratable target) {
		_illustratable = target;
		setDataSet(model);
		List<Image> images = _illustratable.getImages();
		imageList.setImages(images);
	}
	
	private File getImageFile() {
		return getMediaFile(SupportedFileTypes.getSupportedImageFilesFilter());
	}
	
	private File getSoundFile() {
		return getMediaFile(SupportedFileTypes.getSupportedSoundFilesFilter());
	}
	
	private File getMediaFile(FileFilter filter) {
		String imagePath = _dataSet.getImagePath();
		JFileChooser chooser = new JFileChooser(imagePath);
		chooser.setFileFilter(filter);
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File imageFile = chooser.getSelectedFile();
			
			ImageSettings settings = _dataSet.getImageSettings();
			if (settings.isOnResourcePath(imageFile)) {
				
				String name = imageFile.getName();
				boolean exists = false;
				
				if (_selectedImage != null) {
					List<ImageOverlay> existingSounds = _selectedImage.getSounds();
					for (ImageOverlay sound : existingSounds) {
						if (name.equals(sound.overlayText)) {
							exists = true;
							break;
						}
					}
				}
				if (exists) {
					int result = _messageHelper.confirmDuplicateFileName();
					if (result == JOptionPane.YES_OPTION) {
						imageFile = new File(imageFile.getAbsolutePath());
					}
					else if (result == JOptionPane.NO_OPTION) {
						return getMediaFile(filter);
					}
					else {
						imageFile = null;
					}
				}
				else {
					// Turn the file into a relative one.
					imageFile = new File(name);
				}
			}
			else {
				// Ask about it or copy it to the image path.
				int result = _messageHelper.confirmNotOnImagePath();
				if (result == JOptionPane.YES_OPTION) {
					imageFile = new File(imageFile.getAbsolutePath());
				}
				else if (result == JOptionPane.NO_OPTION) {
					return getMediaFile(filter);
				}
				else {
					imageFile = null;
				}
			}
			
			return imageFile;
		}
		
		return null;
	}
	
	/**
	 * Displays the currently selected image.
	 */
	@Action
	public void displayImage() {
		if (_selectedImage == null) {
			return;
		}
		
		ActionMap actions = Application.getInstance().getContext().getActionMap();
		actions.get("viewImageEditor").actionPerformed(null);
	}
	
	/**
	 * Prompts the user to select a file and adds it as an image to the Illustratable object.
	 */
	@Action
	public void addImage() {
		File file = getImageFile();
		if (file == null) {
			return;
		}
		
		_illustratable.addImage(file.getPath(), "");
		
	}
	
	/**
	 * Deletes the currently selected image 
	 */
	@Action
	public void deleteImage() {
		if (_selectedImage == null) {
			return;
		}
		boolean delete = _messageHelper.confirmDeleteImage();
		if (delete) {
			_illustratable.deleteImage(_selectedImage);
		}	
	}
	
	@Action
	public void displayImageSettings() {
		DeltaEditor editor = (DeltaEditor)Application.getInstance();
        editor.viewImageSettings();
	}
	
	/**
	 * Deletes the sound currently selected in the image sound combo box.
	 */
	@Action
	public void deleteSound() {
		ImageOverlay soundFile = (ImageOverlay)soundComboBox.getSelectedItem();
		_selectedImage.deleteOverlay(soundFile);
		soundComboBox.removeItem(soundFile);
	}
	
	/**
	 * Plays the sound currently selected in the image sound combo box.
	 */
	@Action
	public void playSound() {
		ImageOverlay soundFile = (ImageOverlay)soundComboBox.getSelectedItem();
		try {
			URL soundUrl = _dataSet.getImageSettings().findFileOnResourcePath(soundFile.overlayText, false);
			AudioPlayer.playClip(soundUrl);
		}
		catch (Exception e) {
			_messageHelper.errorPlayingSound(soundFile.overlayText);
		}
	}
	
	/**
	 * Allows the user to select a sound file to be added to the image.
	 */
	@Action
	public void addSound() {
		File soundFile = getSoundFile();
		if (soundFile != null) {
			ImageOverlay soundOverlay = new ImageOverlay(OverlayType.OLSOUND);
			soundOverlay.overlayText = soundFile.getPath();
			_selectedImage.addOverlay(soundOverlay);
			soundComboBox.addItem(soundOverlay);
		}
	}
	
	
	private void updateSubjectText() {
		
		String subjectText = subjectTextPane.getText();
		
		updateOverlayText(OverlayType.OLSUBJECT, subjectText);
	}
	
	
	private void updateDeveloperNotes() {
		
		String subjectText = developerNotesTextPane.getText();
		
		updateOverlayText(OverlayType.OLCOMMENT, subjectText);
	}
	
	private void updateSoundActions() {
		boolean enabled = soundComboBox.getSelectedItem() != null;
		_actions.get("playSound").setEnabled(enabled);
		_actions.get("deleteSound").setEnabled(enabled);
		
	}
	
	/**
	 * Updates the overlay text of the supplied overlay.  If the overlay is null,
	 * a new one of the supplied type is created.  If the text is null and the overlay
	 * is not, the overlay is deleted.
	 * @param type the type of overlay to edit.
	 * @param text the new text for the overlay.
	 */
	private void updateOverlayText(int type, String text) {
		if (_selectedImage == null) {
			return;
		}
		ImageOverlay overlay = _selectedImage.getOverlay(type);
		if (overlay == null) {
			if (StringUtils.isNotEmpty(text)) {
				overlay = new ImageOverlay(type);
				overlay.overlayText = text;
				_selectedImage.addOverlay(overlay);
			}
		}
		else {
			if (StringUtils.isNotEmpty(text)) {
				overlay.overlayText = text;
				_selectedImage.updateOverlay(overlay);
			}
			else {
				_selectedImage.deleteOverlay(overlay);
			}	
		}
	}
	
	private void updateDisplay() {
		
		DefaultComboBoxModel model = (DefaultComboBoxModel)soundComboBox.getModel();
		model.removeAllElements();
		if (_selectedImage == null) {
			subjectTextPane.setText("");
			developerNotesTextPane.setText("");
			btnDisplay.setEnabled(false);
			btnDelete.setEnabled(false);
		}
		else {
			btnDisplay.setEnabled(true);
			btnDelete.setEnabled(true);
			List<ImageOverlay> overlays = _selectedImage.getOverlays();
			for (ImageOverlay overlay : overlays) {
			
				if (overlay.isType(OverlayType.OLSUBJECT)) {
					subjectTextPane.setText(overlay.overlayText);
				}
				else if (overlay.isType(OverlayType.OLCOMMENT)) {
					developerNotesTextPane.setText(overlay.overlayText);
				}
				else if (overlay.isType(OverlayType.OLSOUND)) {
					soundComboBox.addItem(overlay);
				}
			}
		}
		updateSoundActions();
	}
	
	/**
	 * Handles drag and drop of Items in the ItemList.
	 */
	class ImageTransferHandler extends SimpleTransferHandler<Image> {
		
		private static final long serialVersionUID = 889705892088002277L;
		
		public ImageTransferHandler() {
			super(Image.class);
		}
		
		public int getSourceActions(JComponent c) {
			return TransferHandler.MOVE;
		}
		
		@Override
		protected Image getTransferObject() {
			return (Image)imageList.getSelectedValue();
		}
		
		@Override
		protected int getStartIndex() {
			return imageList.getSelectedIndex();
		}
		
		@Override
		protected int getDropLocationIndex(DropLocation dropLocation) {
			return ((javax.swing.JList.DropLocation)dropLocation).getIndex();
		}

		@Override
		protected void move(Image image, int targetIndex) {
			_illustratable.moveImage(image, targetIndex);
			imageList.setSelectedIndex(targetIndex);
		}

		@Override
		protected void copy(Image item, int targetIndex) {
			throw new UnsupportedOperationException();
		}
	}

	private DeltaDataSetObserver observer = new AbstractDataSetObserver() {
		@Override
		public void itemEdited(DeltaDataSetChangeEvent event) {
			if (event.getItem().equals(_illustratable)) {
				updateImageList();				
			}
		}

		private void updateImageList() {
			int selection = -1;
			if (_selectedImage != null) {
				selection = imageList.getSelectedIndex();
			}
			bind(_dataSet, _illustratable);
			if (selection != -1) {
				imageList.setSelectedIndex(selection);
			}
		}
		
		@Override
		public void characterEdited(DeltaDataSetChangeEvent event) {
			if (event.getCharacter().equals(_illustratable)) {
				updateImageList();
			}
		}
	};
	
	public void setDataSet(EditorViewModel dataSet) {
		
		if (_dataSet != null) {
			_dataSet.removeDeltaDataSetObserver(observer);
		}
		_dataSet = dataSet;
		_dataSet.addDeltaDataSetObserver(observer);
	}
	
	
}
