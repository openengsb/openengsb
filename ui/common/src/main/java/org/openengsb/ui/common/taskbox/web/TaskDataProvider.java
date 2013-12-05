/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.common.taskbox.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.openengsb.core.workflow.api.TaskboxService;
import org.openengsb.core.workflow.api.model.Task;
import org.ops4j.pax.wicket.api.InjectorHolder;

@SuppressWarnings({ "serial" })
public class TaskDataProvider extends SortableDataProvider<Task, String> implements IFilterStateLocator<Task> {

    @Inject
    @Named("taskboxService")
    private TaskboxService taskboxService;
    private TaskFilter filter = TaskFilter.createTaskFilter();
    private List<Task> list;

    public TaskDataProvider() {
        InjectorHolder.getInjector().inject(this, TaskDataProvider.class);
        this.setSort("taskId", SortOrder.ASCENDING);
    }

    public TaskboxService gett() {
        return taskboxService;
    }

    @Override
    public Iterator<? extends Task> iterator(long first, long count) {
        initList();

        List<Task> ret = list;
        if (ret.size() > first + count) {
            ret = ret.subList((int) first, (int) (first + count));
        } else {
            ret = ret.subList((int) first, ret.size());
        }
        return ret.iterator();
    }

    private void initList() {
        if (list == null) {
            list = getSortedandFilteredList();
        }
    }

    private List<Task> getSortedandFilteredList() {
        List<Task> result = taskboxService.getOpenTasks();

        if (result != null) {
            filterTasks(result);
            Collections.sort(result, new TaskComparator(getSort()));
        } else {
            result = new ArrayList<Task>();
        }
        return result;

    }

    private void filterTasks(List<Task> result) {
        if (filter != null) {
            List<Task> tmp = new ArrayList<Task>();
            for (Iterator<Task> iterator = result.iterator(); iterator.hasNext();) {
                Task task = iterator.next();
                if (!filter.match(task)) {
                    tmp.add(task);
                }
            }
            result.removeAll(tmp);
        }
    }

    @Override
    public long size() {
        initList();
        return list.size();
    }

    @Override
    public void detach() {
        super.detach();
        list = null;
    }

    @Override
    public IModel<Task> model(Task object) {
        return new LoadableTaskModel(object.getTaskId(), taskboxService);
    }

    public void setTaskboxService(TaskboxService taskboxService) {
        this.taskboxService = taskboxService;
    }

    @Override
    public Task getFilterState() {
        return filter;
    }

    @Override
    public void setFilterState(Task state) {
        filter = (TaskFilter) state;
    }

}
