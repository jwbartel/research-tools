<?php
$out = shell_exec('java -jar "prediction_jars/Email Predictions.jar" '.$_POST['id']).' '.$_POST['numMessages'].' '.$_POST['auth_code'].' true';
error_log($out);