package com.gome.idea.plugins.time.tw

import com.gome.idea.plugins.time.http.TimeHttpClient
import com.gome.idea.plugins.time.settings.TimeSettings
import com.gome.idea.plugins.time.ui.TimeToolWindowView
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import org.jetbrains.annotations.NotNull

import javax.swing.*
import javax.swing.event.HyperlinkEvent
/**
 * 工时更新ToolWindow
 * @author xiehai1
 * @date 2017/10/27 11:23
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class TimeToolWindow implements ToolWindowFactory {
    private def static final TOOL_WINDOW_NAME = "Gome Time"

    @Override
    void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        repaint(project, toolWindow)

        // toolWindow 状态变化监听
        ToolWindowManagerEx.getInstanceEx(project).addToolWindowManagerListener(
            new ToolWindowManagerAdapter() {
                @Override
                void stateChanged() {
                    // 对所有打开的idea实例进行reload
                    Project[] projects = ProjectManagerEx.getInstanceEx().getOpenProjects()
                    projects.each { it ->
                        // plugin.xml ToolWindow id
                        ToolWindow tw = ToolWindowManagerEx.getInstance(it).getToolWindow(TOOL_WINDOW_NAME)
                        // 激活ToolWindow做刷新操作
                        if (tw != null && tw.isVisible()) {
                            repaint(it, tw)
                        }
                    }
                }
            }
        )
    }

    def static repaint(Project project, ToolWindow toolWindow) {
        if (!TimeSettings.getInstance().isLegal() || !TimeHttpClient.tryCookie()) {
            final Notification notification = new Notification(
                "Time",
                "Settings",
                "您还未设置Time个人信息!<br/><a href=\"\">立刻前往</a>",
                NotificationType.ERROR,
                new NotificationListener() {
                    @Override
                    public void hyperlinkUpdate(
                        @NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
                        if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Time")
                        }
                    }
                }
            )
            Notifications.Bus.notify(notification, project)
            return
        }
        JComponent component = TimeToolWindowView.getInstance(project).getScrollPane()
        ContentManager contentManager = toolWindow.getContentManager()
        ContentFactory contentFactory = contentManager.getFactory()
        final String contentName = "- Times"
        contentManager.removeAllContents(true)
        contentManager.addContent(contentFactory.createContent(component, contentName, true))
    }
}
