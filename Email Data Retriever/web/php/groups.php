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
        ?>
<!DOCTYPE html>
    <html>
<head>
    <title>E-mail Thread Data Groups</title>
    <link rel="stylesheet" href="groups.css">
    <link rel="stylesheet" href="../js/jquery-ui-1.11.0.custom/jquery-ui.min.css">
    <script src='../js/jquery-1.10.2.min.js' type='text/javascript'></script>
    <script>
    var record_id = '<?php print $record_id; ?>';
    var changes = new Array();

        function addremove(id) {
            if(document.getElementById(id).getAttribute("style")==="background-color:red") {
                document.getElementById(id).setAttribute("style", "background-color:white");
                // insert code to add member to group
                changes.push("Group "+ $('#'+id).parent().parent().attr('id') + ": Added: "+ $('#'+id+'-email').text());
                console.log(changes);
                $('#'+id+'-close').html("Remove");
            }
            else {
                document.getElementById(id).setAttribute("style", "background-color:red");
                // insert code to remove member from group
                changes.push("Group "+ $('#'+id).parent().parent().attr('id') + ": Removed: "+ $('#'+id+'-email').text());
                console.log(changes);
                $('#'+id+'-close').html("Undo");
                $('#'+id).remove();
            }
        }
        function deletegroup(id) {
            changes.push("Group " + id + ": Delete Group");
            console.log(changes);
            $('#'+id).remove();
        }
        function submitData() {
            $.ajax({
                type: "POST",
                url: "update_groups.php",
                data: {r:record_id, edits:changes},
                success: function(data, textStatus, jqXHR) {
                    console.log(textStatus);
                    window.location.href = "https://docs.google.com/forms/d/1jgkrkOdFAs9vEVmPYjTI-kbjbS-8ZIhFC9ORBacLoX0/viewform?usp=send_form";
}
                
        })
}
        var otherId = -1;
        function addOther(id) {
            var otherAddress = $('#'+id+'-other').val();
            changes.push("Group " + id + ": Added: " + otherAddress);
            console.log(changes);
            otherId--;

            $('#'+id+'-buttons').append('<div class="button" id="'+otherId+'"> <div class="email_address" id="'+otherId+'-email"> '+otherAddress+'</div> <div class="close" id="'+otherId+'-close" onClick="addremove(\''+otherId+'\')">Remove</div></div> ');
        }
        var customIdNumber = 1;
        function addGroup(name) {
            var customId = 'custom-'+customIdNumber;
            var newGroupCode = '';
            newGroupCode += '<div id="'+customId+'">';
            newGroupCode += '<b>Group Name: </b> <input type="text" value="'+name+'" id="'+customId+'-name"><br>';
            newGroupCode += '<button type="button" onClick="deletegroup(\''+customId+'\')">Delete Group</button><br>';
            newGroupCode += '<div id="'+customId+'-buttons">';      
            newGroupCode += '</div>';
            newGroupCode += '<br> Other: <input class="otherAddress" type="text" id="'+customId+'-other"/>';
            newGroupCode += '<button type="button" onClick="addOther(\''+customId+'\')">Add</button>';
            newGroupCode += '</div>';
            newGroupCode += '<br><br><br>';

            $('#allGroups').append(newGroupCode);
            $('#'+customId+'-name').on('input', function() {
                changes.push('Group '+ customId+' Name Typed: ' +$('#'+customId+'-name').val());
                console.log(changes);
            });
            $('#'+customId+'-other').on('input', function() {
                changes.push('Group '+ customId+' Other Email Typed: ' +$('#'+customId+'-other').val());
                console.log(changes);
             });
            customIdNumber++;
        }
    </script>
</head>
<body>
    <div class="center" id="reviewer">
       
            <?php
            writeData($groups_exist, $groups_data, 'Predicted Groups', '100%', false);
            function writeData($exists, $data, $label, $width) {
                if ($exists) {
                    print('<h3>'.$label.'</h3>');
                    print '<form id="allGroups">';
                    foreach($data as $key=>$group) {
                        print '<div id="'.($key+1).'">';
                        print '<b>Group Name: </b> <input type="text" id="'.($key+1).'-name"><br>'."\n";
                        print "<script> $('#".($key+1)."-name').on('input', function() {
            changes.push('Group '+ '".($key+1)."'+' Name Typed: ' +$('#".($key+1)."-name').val());
            console.log(changes);
        });  </script>";
                        print '<button type="button" onClick="deletegroup(\''.($key+1).'\')">Delete Group</button><br>';
                        print '<div id="'.($key+1).'-buttons">';
                        foreach($group as $entry)
                            writeButton($entry);
                        print '</div>';
                        print '<br> Other: <input class="otherAddress" type="text" id="'.($key+1).'-other"/>';
                        print "<script> $('#".($key+1)."-other').on('input', function() {
            changes.push('Group '+ '".($key+1)."'+' Other Email Typed: ' +$('#".($key+1)."-other').val());
            console.log(changes);
        });  </script>";
                        print '<button type="button" onClick="addOther(\''.($key+1).'\')">Add</button>';
                        print('<br><br><br>'."\n");
                        print '</div>';
                    }
                        print '</form>';
                    ?>
                        <h3> Add Custom Group</h3>
                        <div id="AddGroup">
                           <b>Group Name: </b> <input type="text" id="custom-name"/><br>
                            <button type="button" id="addGroupButton" onClick="addGroup($('#custom-name').val())">Add Group</button>
                            <script> 
                                $('#custom-name').on('input', function() {
                                   changes.push('Custom Group Name Typed: ' + $('#custom-name').val());
                                   console.log(changes);
                                  }); 
                            </script>
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