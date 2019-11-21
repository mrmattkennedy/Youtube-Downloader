<?php
	if(isset($_POST['dl'])) 
	{
		$file_url = 'file:///C:/Users/mkennedy2/Documents/Scripts/Youtube-Downloader/Website/test.mp4';
		header('Content-Type: application/octet-stream');
		header("Content-Transfer-Encoding: Binary"); 
		header("Content-disposition: attachment; filename=\"" . basename($file_url) . "\""); 
		readfile($file_url); // do the double-download-dance (dirty but worky)
	}
?>