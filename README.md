Mail Listener Project
================================================================================

<b>Description:</b><br>
This project listen an email from mailbox and create a task on readmine pm tool. <br>
Project was developed as ESB project and you should deploy it with JbossESB <br>

<b>Using:</b><br>
1. Setup your email in file spring-beans.xml<br>
2. Deploy project in AS as JbossESB<br>
3. Send an email to this email. Service will create an issue/task on redmine PM tool which:<br>
  - Issue created in project which configured in email.properties
  - Name is mail's subject.
  - Content is mail's content.
  - You can add more attachments.
  - The first mail address in CC list will be chosen to assign this issue/task to.
