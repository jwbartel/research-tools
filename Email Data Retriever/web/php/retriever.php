Returning this will indicate that the retrieval process has completed.  It should also redirect to a UI to view the submitted data.

<?php 
	print exec('java -jar EmailDataRetriever.jar');
?>