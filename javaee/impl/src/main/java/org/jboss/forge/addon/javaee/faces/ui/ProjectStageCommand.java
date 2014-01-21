/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.javaee.faces.ui;

import javax.faces.application.ProjectStage;
import javax.inject.Inject;

import org.jboss.forge.addon.javaee.faces.FacesFacet;
import org.jboss.forge.addon.javaee.servlet.ServletFacet;
import org.jboss.forge.addon.javaee.ui.AbstractJavaEECommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class ProjectStageCommand extends AbstractJavaEECommand
{

   @Inject
   @WithAttributes(label = "")
   private UISelectOne<ProjectStage> set;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(set);
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("Faces: Project Stage")
               .description("Set the project stage of this JSF project")
               .category(Categories.create(super.getMetadata(context).getCategory(), "JSF"));
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      final Result result;
      Project project = getSelectedProject(context.getUIContext());
      FacesFacet facesFacet = project.getFacet(FacesFacet.class);
      if (set.hasValue())
      {
         ProjectStage projectStage = set.getValue();
         facesFacet.setProjectStage(projectStage);
         result = Results.success("Faces PROJECT_STAGE updated to: " + projectStage);
      }
      else
      {
         result = Results.success("Project stage is currently: " + facesFacet.getProjectStage());
      }
      return result;
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      boolean enabled = super.isEnabled(context);
      if (enabled)
      {
         final Project project = getSelectedProject(context);
         enabled = project.hasFacet(ServletFacet.class) && project.hasFacet(FacesFacet.class);
      }
      return enabled;
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

}
