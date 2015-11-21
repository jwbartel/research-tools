<?php
        $record_id = $_GET['r'];
        $private_folder= '/afs/cs.unc.edu/home/andrewwg/email_threads/private_data/'.$record_id;

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
        $messages_file = $private_folder.'/labelable_message.txt';
        $messages_exist = file_exists($messages_file);
        $messages_data = array();
        if ($messages_exist) {
            $file_handle = fopen($messages_file, "r");
            while (!feof($file_handle)) {
                $messages_line = fgets($file_handle);
                if(trim($messages_line)!=='') {
                    $a_group = explode("\t",$messages_line);
                    array_push($messages_data,$a_group);
                }
            }
            fclose($file_handle);
        }

        ?>
<!DOCTYPE html>
    <html>
<head>
    <title>E-mail Labels</title>
    <link rel="stylesheet" href="groups.css">
    <link rel="stylesheet" href="../js/jquery-ui-1.11.0.custom/jquery-ui.min.css">
    <script src='../js/jquery-1.10.2.min.js' type='text/javascript'></script>
    <script>
    var record_id = '<?php print $record_id; ?>';
    var numMessages = '<?php print $_GET['numMessages']; ?>';
    var auth_code = '<?php print $_GET['auth_code']; ?>';

    var changes = new Array();

        function addremove(id) {
            if(document.getElementById(id).getAttribute("style")==="background-color:red") {
                document.getElementById(id).setAttribute("style", "background-color:white");
                // insert code to add member to group
                changes.push("Label "+ $('#'+id).parent().parent().attr('id') + ": Added: "+ $('#'+id+'-email').text());
                console.log(changes);
                $('#'+id+'-close').html("Remove");
            }
            else {
                document.getElementById(id).setAttribute("style", "background-color:red");
                // insert code to remove member from group
                changes.push("Label "+ $('#'+id).parent().parent().attr('id') + ": Removed: "+ $('#'+id+'-email').text());
                console.log(changes);
                $('#'+id+'-close').html("Undo");
                $('#'+id).remove();
            }
        }
        function deletegroup(id) {
            changes.push("Label " + id + ": Delete Label");
            console.log(changes);
            $('#'+id).remove();
        }
        function submitData() {
            $.ajax({
                type: "POST",
                url: "apply_labels.php",
                data: {id:record_id, numMessages:numMessages,auth_code:auth_code},
                success: function(data, textStatus, jqXHR) {
                    alert("applay labels ajax:" +textStatus);
}

        })
}
        var otherId = -1;
        function addOther(id) {
            var otherAddress = $('#'+id+'-other').val();
            changes.push("Label " + id + ": Added: " + otherAddress);
            console.log(changes);
            otherId--;

            $('#'+id+'-buttons').append('<div class="button" id="'+otherId+'"> <div class="email_address" id="'+otherId+'-email"> '+otherAddress+'</div> <div class="close" id="'+otherId+'-close" onClick="addremove(\''+otherId+'\')">Remove</div></div> ');
        }
        var customIdNumber = 1;
        function addLabel(name) {
            var customId = 'custom-'+customIdNumber;
            var newLabelCode = '';
            newLabelCode += '<div id="'+customId+'">';
            newLabelCode += '<b>Label Name: </b> <input type="text" value="'+name+'" id="'+customId+'-name"><br>';
            newLabelCode += '<button type="button" onClick="deletegroup(\''+customId+'\')">Delete Label</button><br>';
            newLabelCode += '<div id="'+customId+'-buttons">';
            newLabelCode += '</div>';
            newLabelCode += '<br> Other: <input class="otherAddress" type="text" id="'+customId+'-other"/>';
            newLabelCode += '<button type="button" onClick="addOther(\''+customId+'\')">Add</button>';
            newLabelCode += '</div>';
            newLabelCode += '<br><br><br>';

            $('#allLabels').append(newLabelCode);
            $('#'+customId+'-name').on('input', function() {
                changes.push('Label '+ customId+' Name Typed: ' +$('#'+customId+'-name').val());
                console.log(changes);
            });
            $('#'+customId+'-other').on('input', function() {
                changes.push('Label '+ customId+' Other Email Typed: ' +$('#'+customId+'-other').val());
                console.log(changes);
             });
            customIdNumber++;
        }
        function addSkippedMessage(message_id) {
//            alert(message_id);
            $.ajax({
                type: "POST",
                url: "addSkippedMessage.php",
                data: {r:record_id, messageID:message_id},
                success: function(data, textStatus, jqXHR) {
                    console.log('message '+message_id+ ' added to skip list');
                    $('#remove-button-'+message_id).prop('disabled', true);
                }

            })
        }
    </script>
</head>
<body>
    <div class="center" id="reviewer">

            <?php
            writeData($groups_exist, $groups_data, 'Predicted Labels');
            function writeData($exists, $data, $label) {
                if ($exists) {
                    print('<h3>'.$label.'</h3>');
                    print '<form id="allLabels">';
                    foreach($data as $key=>$group) {
                        print '<div id="'.($key+1).'">';
                        print '<b>Label Name: </b> <input type="text" id="'.($key+1).'-name"><br>'."\n";
                        print "<script> $('#".($key+1)."-name').on('input', function() {
            changes.push('Label '+ '".($key+1)."'+' Name Typed: ' +$('#".($key+1)."-name').val());
            console.log(changes);
        });  </script>";
                        print '<button type="button" onClick="deletegroup(\''.($key+1).'\')">Delete Label</button><br>';
                        print '<div id="'.($key+1).'-buttons">';
                        foreach($group as $entry)
                            writeButton($entry);
                        print '</div>';
                        print '<br> Other: <input class="otherAddress" type="text" id="'.($key+1).'-other"/>';
                        print "<script> $('#".($key+1)."-other').on('input', function() {
            changes.push('Label '+ '".($key+1)."'+' Other Email Typed: ' +$('#".($key+1)."-other').val());
            console.log(changes);
        });  </script>";
                        print '<button type="button" onClick="addOther(\''.($key+1).'\')">Add</button>';
                        print('<br><br><br>'."\n");
                        print '</div>';
                    }
                        print '</form>';
                    ?>
                        <h3> Add Custom Label</h3>
                        <div id="AddLabel">
                           <b>Label Name: </b> <input type="text" id="custom-name"/><br>
                            <button type="button" id="addLabelButton" onClick="addLabel($('#custom-name').val())">Add Label</button>
                            <script>
                                $('#custom-name').on('input', function() {
                                   changes.push('Custom Label Name Typed: ' + $('#custom-name').val());
                                   console.log(changes);
                                  });
                            </script>
                         </div><br>
                        <h3> Messages to be labeled</h3>
                        <div>
                            <?php
                                global $messages_data;
                                print '<table border="1">';
                                print '<tr><th>From</th><th>Recipients</th><th>Subject</th><th>Label</th><th>Remove?</th></tr>';
                            foreach($messages_data as $key=>$group) {
                                    print '<tr>';
                                    foreach($group as $i_key=>$item) {
                                        print '<td>';
                                        if($i_key==4) {print "\n";
                                            print "<button id=\"remove-button-".trim($item)."\" onclick=\"addSkippedMessage('".trim($item)."')\">Remove</button>";
                                            print "\n";
                                        } else {
                                            print $item;
                                        }
                                        print '</td>';
                                    }
                                    print '</tr>';
                                }
                            print '</table>';
                            ?>
                        </div><br>
                    <?php
                    print '<button type="button" style="width:300px" onClick="submitData()">Submit and Continue</button>';


                }
            }
            ?>

        <br>
        <?php
        function writeButton ($email_address) {
            $id = uniqid();
            print "\t".'<div class = "button" id="'.$id.'">';
                    print '<div id="'.$id.'-email" class="email_address">'.$email_address.'</div>';
                    print " ".'<div class="close" id="'.$id.'-close"  onClick="addremove(\''.$id.'\')">Remove</div>'."\n";
            print "\t".'</div>'."\n";
            print '<script>$("#'.$id.'").css("height",$("#'.$id.'-email").height())</script>';
        }
        ?>

    </div>
</body>
    </html>
    <script src='../js/jquery-ui-1.11.0.custom/jquery-ui.min.js' type='text/javascript'></script>
    <script>
    var allEmails = [
    <?php foreach($groups_data as $key=>$group) {
            foreach($group as $entry) {
                print '"'.$entry.'",';
            }
        }
        ?>
        ""];
    $(function() {
    $( ".otherAddress" ).autocomplete({
      source: allEmails
    });
  });</script>