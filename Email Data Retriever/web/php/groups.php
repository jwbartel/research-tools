<!DOCTYPE html>
    <html>
<head>
    <title>E-mail Thread Data Groups</title>
    <link rel="stylesheet" href="groups.css">
    <script>
        function addremove(id) {
            if(document.getElementById(id).getAttribute("style")==="background-color:red") {
                document.getElementById(id).setAttribute("style", "background-color:lightgreen");
                // insert code to add member to group
            }
            else {
                document.getElementById(id).setAttribute("style", "background-color:red");
                // insert code to remove member from group
            }
        }

    </script>
</head>
<body>
    <div class="center" id="reviewer">
        <?php
        $record_id = $_GET['r'];
        $private_folder= '/afs/cs.unc.edu/home/bartel/email_threads/private_data/'.$record_id;

        $groups_file = $private_folder.'/groups.txt';
        $groups_exist = file_exists($groups_file);

        $groups_data = array();
        if ($groups_exist) {
            $file_handle = fopen($groups_file, "r");
            while (!feof($file_handle)) {
                $group_line = fgets($file_handle);
                if(trim($group_line)!=='') {
                    $a_group = explode(",",substr($group_line,1, strlen($group_line)-3));
                    array_push($groups_data,$a_group);
                }
            }
            fclose($file_handle);
        }
        ?>
        <table style='width:100%'>
            <?php
            writeData($groups_exist, $groups_data, 'Predicted Groups', '100%', false);
            function writeData($exists, $data, $label, $width) {
                if ($exists) {
                    print('<td style="width:'.$width.'">');
                    print('<h3>'.$label.'</h3>');
                    print '<form>';
                    foreach($data as $group) {
                        print '<b>Group Name: </b> <input type="text" ><br>'."\n";
                        foreach($group as $entry)
                            writeButton($entry);
                        print('<br><br>'."\n");
                    }
                    print '<button type="submit" formaction="update_groups.php">Submit and Continue</button>';
                    print '</form>';
                    print('</td>');
                }
            }
            ?>
        </table>
        <br>
        <?php
        function writeButton ($email_address) {
            $id = uniqid();
            print "\t".'<div class = "button" id="'.$id.'">';
                    print "\t\t".$email_address;
                    print "\t\t".'<div class="close" onClick="addremove(\''.$id.'\')">+/-</div>'."\n";
            print "\t".'</div>'."\n";
        }
        ?>
    </div>
</body>
    </html>