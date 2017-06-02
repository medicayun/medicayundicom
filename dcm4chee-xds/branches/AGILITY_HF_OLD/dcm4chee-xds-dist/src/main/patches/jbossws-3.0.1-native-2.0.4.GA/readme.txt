This JBossWS version already contains the patches for correct XOP optimization handling.
The required patches are:
1) Patch to handle incorrect AXIS 2 SwA requests. (AttachmentPart Id mismatch)
2) Fix 'Internet required' issue. 
   (JBoss added a schema import handling that requires internet access for schema vaildatiion) 