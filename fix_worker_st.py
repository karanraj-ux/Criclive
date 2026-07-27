import re
with open('app/src/main/java/com/example/widget/WidgetUpdateWorker.kt', 'r') as f:
    content = f.read()

content = content.replace('views.setTextViewText(R.id.widget_status, "NOT FOUND (PINNED)")', 
                          'views.setTextViewText(R.id.widget_status, st)')

with open('app/src/main/java/com/example/widget/WidgetUpdateWorker.kt', 'w') as f:
    f.write(content)
